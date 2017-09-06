import os
import yaml
import sys

from wand.image import Image
from wand.font import Font
from wand.color import Color

from colorama import Fore

def wrap_screenshot (src, bg, offsets, output):
	left   = offsets["left"]
	top    = offsets["top"]
	right  = offsets["right"]
	bottom = offsets["bottom"]

	height = bottom - top
	width  = right - left

	with Image(filename=src) as screen, Image(filename=bg) as background:
		screen.resize(width, height)
		background.composite(screen, left=left, top=top)
		background.save(filename=output)
		print(Fore.GREEN + '[OK] Screenshot ' + src + ' wrapped as ' + output)


base_dir = sys.argv[1] if len(sys.argv) > 1 is not None else '.'
platform = sys.argv[2] if len(sys.argv) > 2 is not None else 'phone'


with open('platforms.yml', 'r') as platforms:
    try:
        platforms = yaml.load(platforms)
    except yaml.YAMLError as exc:
        print(exc)


with open('config.yml', 'r') as config:
     try:
         config = yaml.load(config)
     except yaml.YAMLError as exc:
         print(exc)

for locale in config['locales']:
	base_prefix = base_dir + '/' + locale + '/'

	screenshots_dir = base_prefix + config['screenshots_dir'] + '/' + config['input_dir'][platform] + '/'
	templates_dir = base_prefix + config['templates_dir'] + '/' + config['input_dir'][platform] + '/'

	screenshots = [a for a in next(os.walk(screenshots_dir))[2] if not a.startswith('.')]
	templates = [a for a in next(os.walk(templates_dir))[2] if not a.startswith('.')]

	output_dir = base_prefix + config['output_dir'][platform] + '/'

	for i in range(0, len(screenshots)):
		screenshot = screenshots[i]
		template = templates[i]
		print(Fore.YELLOW + 'Wrapping screenshot ' + screenshots_dir + screenshot + ' with template ' + templates_dir + template)
		wrap_screenshot(screenshots_dir + screenshot, templates_dir + template, platforms[platform]["offsets"], output_dir + screenshot)
		


# wrap_screenshot ('./01.png', './market_screenshot_template.png', devices[0]["offsets"], './001.png')

