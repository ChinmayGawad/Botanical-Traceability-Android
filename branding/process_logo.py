import os
import numpy as np
from PIL import Image, ImageFilter

def make_transparent(input_path, output_path):
    img = Image.open(input_path).convert('RGBA')
    arr = np.array(img, dtype=np.float32)
    
    # Calculate distance from pure white (255, 255, 255)
    r, g, b, a = arr[:, :, 0], arr[:, :, 1], arr[:, :, 2], arr[:, :, 3]
    
    # White background detection:
    # If a pixel is very close to white (brightness > 248 and saturation < 15), make it transparent
    max_val = np.maximum(np.maximum(r, g), b)
    min_val = np.minimum(np.minimum(r, g), b)
    diff = max_val - min_val
    
    # Whiteness: high brightness and low saturation
    is_white = (min_val > 240) & (diff < 20)
    
    # For anti-aliasing near the edge:
    # Smooth transition between 220 and 250
    alpha = np.ones_like(r) * 255.0
    
    # Where pixel is nearly white, calculate alpha smoothly
    fade_mask = (min_val >= 220) & (diff < 35)
    # The closer to 255, the lower the alpha
    fade_factor = np.clip((255.0 - min_val) / 35.0, 0.0, 1.0)
    alpha[fade_mask] = 255.0 * fade_factor[fade_mask]
    alpha[is_white] = 0.0
    
    # For dark green foreground pixels, restore color integrity if it was slightly washed by white
    # Color un-multiplication where alpha < 255
    foreground_mask = (alpha > 0) & (alpha < 255)
    scale = 255.0 / np.maximum(alpha[foreground_mask], 1.0)
    # Recover pure foreground color
    r[foreground_mask] = np.clip(255.0 - (255.0 - r[foreground_mask]) * scale, 0, 255)
    g[foreground_mask] = np.clip(255.0 - (255.0 - g[foreground_mask]) * scale, 0, 255)
    b[foreground_mask] = np.clip(255.0 - (255.0 - b[foreground_mask]) * scale, 0, 255)
    
    out_arr = np.stack([r, g, b, alpha], axis=2).astype(np.uint8)
    out_img = Image.fromarray(out_arr, 'RGBA')
    out_img.save(output_path, 'PNG')
    print(f"Saved transparent logo: {output_path}")
    return out_img

def generate_web_pack(transparent_img, web_dir):
    os.makedirs(web_dir, exist_ok=True)
    
    # Square cropped to tight bounds
    bbox = transparent_img.getbbox()
    cropped = transparent_img.crop(bbox)
    
    # Add comfortable 10% padding so it sits beautifully in favicons/icons
    pad = int(max(cropped.size) * 0.12)
    padded_size = (cropped.width + pad * 2, cropped.height + pad * 2)
    padded = Image.new('RGBA', padded_size, (0, 0, 0, 0))
    padded.paste(cropped, (pad, pad), cropped)
    
    sizes = {
        'favicon-16x16.png': (16, 16),
        'favicon-32x32.png': (32, 32),
        'apple-touch-icon.png': (180, 180),
        'android-chrome-192x192.png': (192, 192),
        'android-chrome-512x512.png': (512, 512),
        'logo-512x512.png': (512, 512),
    }
    
    for filename, size in sizes.items():
        resized = padded.resize(size, Image.Resampling.LANCZOS)
        out_path = os.path.join(web_dir, filename)
        resized.save(out_path, 'PNG')
        print(f"Generated web asset: {filename} ({size[0]}x{size[1]})")
        
    # Generate favicon.ico with multi-resolutions
    ico_img16 = padded.resize((16, 16), Image.Resampling.LANCZOS)
    ico_img32 = padded.resize((32, 32), Image.Resampling.LANCZOS)
    ico_img48 = padded.resize((48, 48), Image.Resampling.LANCZOS)
    ico_path = os.path.join(web_dir, 'favicon.ico')
    ico_img32.save(ico_path, format='ICO', sizes=[(16, 16), (32, 32), (48, 48)])
    print(f"Generated multi-res favicon.ico")

def generate_android_pack(transparent_img, app_res_dir, design_res_dir):
    # Android launcher icons: standard square and circular
    # In Android, app icon usually has a subtle clean background (e.g. pure white #FFFFFF or dark #0B131E)
    bbox = transparent_img.getbbox()
    cropped = transparent_img.crop(bbox)
    
    # Adaptive Icon Foreground: 432x432 with safe area of 264x264 centered (108dp canvas with 66dp safe zone)
    foreground = Image.new('RGBA', (432, 432), (0, 0, 0, 0))
    # Resize cropped mark to fit within safe zone of 260x260
    mark_safe = cropped.copy()
    mark_safe.thumbnail((260, 260), Image.Resampling.LANCZOS)
    offset_x = (432 - mark_safe.width) // 2
    offset_y = (432 - mark_safe.height) // 2
    foreground.paste(mark_safe, (offset_x, offset_y), mark_safe)
    
    # Save adaptive foreground to drawable
    drawable_dir = os.path.join(app_res_dir, 'drawable')
    os.makedirs(drawable_dir, exist_ok=True)
    fg_path = os.path.join(drawable_dir, 'ic_launcher_foreground.png')
    foreground.save(fg_path, 'PNG')
    print(f"Generated Android adaptive foreground: {fg_path}")
    
    # Also save in design-system res drawable for in-app UI
    design_drawable_dir = os.path.join(design_res_dir, 'drawable')
    os.makedirs(design_drawable_dir, exist_ok=True)
    inapp_logo = cropped.resize((512, 512), Image.Resampling.LANCZOS)
    inapp_logo.save(os.path.join(design_drawable_dir, 'ic_florachain_logo.png'), 'PNG')
    print("Saved in-app logo drawable")
    
    # Generate standalone legacy / fallback launcher icons (both square and round)
    # Background for launcher icon: Clean white or dark. Let's create both or white for maximum contrast with green mark!
    # Android standard densities:
    densities = {
        'mipmap-mdpi': 48,
        'mipmap-hdpi': 72,
        'mipmap-xhdpi': 96,
        'mipmap-xxhdpi': 144,
        'mipmap-xxxhdpi': 192,
    }
    
    for folder, size in densities.items():
        mip_dir = os.path.join(app_res_dir, folder)
        os.makedirs(mip_dir, exist_ok=True)
        
        # 1. Square Icon with rounded squircle background
        sq_icon = Image.new('RGBA', (size, size), (255, 255, 255, 255))
        # Mask icon mark
        target_mark_size = int(size * 0.76)
        scaled_mark = cropped.copy()
        scaled_mark.thumbnail((target_mark_size, target_mark_size), Image.Resampling.LANCZOS)
        m_x = (size - scaled_mark.width) // 2
        m_y = (size - scaled_mark.height) // 2
        sq_icon.paste(scaled_mark, (m_x, m_y), scaled_mark)
        sq_icon.save(os.path.join(mip_dir, 'ic_launcher.png'), 'PNG')
        
        # 2. Round Icon
        mask = Image.new('L', (size, size), 0)
        from PIL import ImageDraw
        draw = ImageDraw.Draw(mask)
        draw.ellipse((0, 0, size - 1, size - 1), fill=255)
        
        round_icon = Image.new('RGBA', (size, size), (255, 255, 255, 255))
        round_icon.paste(scaled_mark, (m_x, m_y), scaled_mark)
        round_icon.putalpha(mask)
        round_icon.save(os.path.join(mip_dir, 'ic_launcher_round.png'), 'PNG')
        print(f"Generated {folder}: ic_launcher.png ({size}x{size}) & round")

if __name__ == '__main__':
    src = 'branding/florachain_logo_master.jpg'
    transparent_path = 'branding/florachain_logo_transparent.png'
    trans_img = make_transparent(src, transparent_path)
    
    generate_web_pack(trans_img, 'branding/web')
    generate_android_pack(trans_img, 'app/src/main/res', 'design-system/src/main/res')
    print("ALL LOGO ASSETS GENERATED SUCCESSFULLY!")
