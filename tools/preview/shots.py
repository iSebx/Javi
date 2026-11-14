import sys
from playwright.sync_api import sync_playwright
d = sys.argv[1]
with sync_playwright() as p:
    b = p.chromium.launch(executable_path="/usr/bin/google-chrome", args=["--no-sandbox"])
    pg = b.new_page(viewport={"width": 800, "height": 600}, device_scale_factor=1.5)
    for n in sys.argv[2:]:
        pg.goto("file://%s/%s.html" % (d, n)); pg.wait_for_timeout(200)
        pg.screenshot(path="%s/%s.png" % (d, n), full_page=True)
    b.close()
