# FloraChain • Brand Identity & Design System Assets

> **"Soil-to-Shelf Botanical Traceability on Blockchain"**  
> Visual Identity uniting Ancient Ayurvedic Botanical Purity with Modern Cryptographic Consensus.

---

## 🌿 The Logo Concept
The **FloraChain** emblem embodies the seamless union between nature and technology:
- **Left Half (Nature & Origin)**: Flowing, organic botanical leaf lobes celebrating herbal medicinal life (Ashwagandha, Tulsi, Brahmi).
- **Right Half (Proof & Traceability)**: Geometric circuit traces and telemetry sensor nodes branching outward into the network.
- **Hexagonal Frame**: The cryptographic block cipher representing the decentralized Polygon blockchain ledger.

---

## 🎨 Color Palette

| Token | Hex | Role | Usage |
| :--- | :--- | :--- | :--- |
| **Forest Green** | `#0A3D2A` | Primary Brand | Headings, primary brand typography, high-contrast badges |
| **Botanical Emerald** | `#10B981` | Accent / Status | Primary buttons, verification badges, active states |
| **Electric Mint** | `#00F5A0` | Neon Circuit | Apex nodes, cryptographic highlights, circuit glow |
| **Warm Ivory (Light)** | `#FBFBFA` | Light Background | Soft paper luxury background (Apple / Notion style) |
| **Pure White** | `#FFFFFF` | Light Surface | Elevated cards, dialogs, inputs in Light Mode |
| **Cyber Slate (Dark)** | `#0B131E` | Dark Background | Futuristic obsidian dark mode canvas |
| **Muted Slate** | `#4A5D54` | Secondary Text | Subtitles, descriptions, metadata |

---

## 📁 Asset Directory Structure

```
branding/
├── index.html                           # Interactive Web Asset Showcase & Quick-Download Hub
├── florachain_mark.svg                  # Master Vector Mark (Square 512x512)
├── florachain_logo_horizontal.svg       # Horizontal Lockup for Light Websites & Headers
├── florachain_logo_horizontal_dark.svg  # Horizontal Lockup for Dark Websites & Hero Sections
├── florachain_logo_master.jpg           # Original 1024x1024 Master Artwork
├── florachain_logo_transparent.png      # 1024x1024 Transparent HD PNG (Zero Background)
└── web/                                 # Ready-to-deploy Website & PWA Asset Suite
    ├── favicon-16x16.png
    ├── favicon-32x32.png
    ├── favicon.ico                      # Multi-resolution Windows / Browser ICO (16, 32, 48)
    ├── apple-touch-icon.png             # iOS Home Screen Icon (180x180)
    ├── android-chrome-192x192.png       # Android PWA Icon (192x192)
    ├── android-chrome-512x512.png       # Android PWA Splash Icon (512x512)
    └── logo-512x512.png                 # Web App Icon
```

---

## 🌐 How to Use on Any Website (React / Next.js / HTML)

### 1. In your HTML `<head>`:
```html
<link rel="icon" type="image/png" sizes="32x32" href="/favicon-32x32.png">
<link rel="icon" type="image/png" sizes="16x16" href="/favicon-16x16.png">
<link rel="apple-touch-icon" sizes="180x180" href="/apple-touch-icon.png">
<link rel="icon" type="image/svg+xml" href="/florachain_mark.svg">
```

### 2. In your Website Navigation Bar:
```html
<!-- Light Navigation Bar -->
<img src="/florachain_logo_horizontal.svg" alt="FloraChain" height="40" />

<!-- Dark Navigation Bar -->
<img src="/florachain_logo_horizontal_dark.svg" alt="FloraChain" height="40" />
```

---

## 📱 Android App Integration

- **Launcher Icon**: Automatically configured in `app/src/main/res/` across `mipmap-mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi` as well as adaptive vector foreground `ic_launcher_foreground.png`.
- **Top Bar**: Rendered via `R.drawable.ic_florachain_logo` in `AppTopBar.kt`.
- **Login Screen**: Featured prominently at 76dp in `LoginScreen.kt`.
