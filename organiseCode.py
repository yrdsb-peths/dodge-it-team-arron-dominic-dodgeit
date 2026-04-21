import os

# Files or directories to ignore
IGNORE_LIST = ['project.lock']

def collect_java_files():
    output_file = "all_project_code.txt"
    
    with open(output_file, "w", encoding="utf-8") as outfile:
        # Walk through the current directory
        for root, dirs, files in os.walk("."):
            for file in files:
                if file.endswith(".java") and file not in IGNORE_LIST:
                    full_path = os.path.join(root, file)
                    
                    outfile.write(f"\n{'='*50}\n")
                    outfile.write(f"FILE: {full_path}\n")
                    outfile.write(f"{'='*50}\n\n")
                    
                    try:
                        with open(full_path, "r", encoding="utf-8") as infile:
                            outfile.write(infile.read())
                    except Exception as e:
                        outfile.write(f"Could not read file: {e}")
                    
                    outfile.write("\n\n")
    
    print(f"Success! All code has been bundled into: {output_file}")

if __name__ == "__main__":
    collect_java_files()