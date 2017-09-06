import os
import yaml
import sys
import shutil

# def move_screenshots_after_screengrab():
	


base_dir = sys.argv[1] if len(sys.argv) > 1 is not None else '.'
platform = sys.argv[2] if len(sys.argv) > 2 is not None else 'phone'

with open('config.yml', 'r') as config:
     try:
         config = yaml.load(config)
     except yaml.YAMLError as exc:
         print(exc)



for locale in config['locales']:
        base_prefix = base_dir + '/' + locale + '/'
	
	screengrab_dir = base_prefix + config['output_dir'][platform] + '/'	
	screenshots_dir = base_prefix + config['screenshots_dir'] + '/' + config['input_dir'][platform] + '/'	

	screenshots = [a for a in next(os.walk(screengrab_dir))[2] if not a.startswith('.')]
	
	if not os.path.exists(screenshots_dir):
    		os.makedirs(screenshots_dir)

	

	for screenshot in screenshots:
		print('Move from ' + screengrab_dir + screenshot + ' to ' + screenshots_dir + screenshot)
		shutil.move(screengrab_dir + screenshot, screenshots_dir + screenshot)


