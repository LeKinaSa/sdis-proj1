import subprocess, pathlib, os

os.chdir(pathlib.Path(__file__).parent)

directories = [".", "clientCommands"]

subprocess.check_call(" ".join(["javac"] + list(map(lambda x : x + "/*.java", directories)) + ["-d", "../out"]), shell=True)