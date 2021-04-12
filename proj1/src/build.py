import subprocess, pathlib, os, shutil, sys 

src = pathlib.Path(__file__).parent
out = src.parent / 'out'

os.chdir(src)

try:
    shutil.rmtree(out / 'peer') # Remove what was there before
except FileNotFoundError:
    pass

if sys.platform == 'win32':
    command = "javac peer/*.java peer/clientCommands/*.java peer/messages/*.java peer/state/*.java -d ../out"
else:
    command = "javac peer/*.java peer/clientCommands/*.java peer/messages/*.java peer/state/*.java -d ../out"

print(command)
subprocess.check_call(command, shell=True)