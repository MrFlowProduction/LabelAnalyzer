from PIL import Image
import os, sys

icon_dir = "src/main/resources/hu/mrflow/labelanalyzer/icon"
sizes = [16, 24, 32, 48, 64, 128, 256]
images = []
for size in sizes:
    path = f"{icon_dir}/icon-{size}.png"
    if os.path.exists(path):
        img = Image.open(path).convert("RGBA")
        images.append(img)
        print(f"Added: {path}")

if not images:
    print("ERROR: No PNG icons found!")
    sys.exit(1)

images[0].save("app.ico", format="ICO",
    append_images=images[1:],
    sizes=[(img.width, img.height) for img in images])
print("app.ico created successfully")