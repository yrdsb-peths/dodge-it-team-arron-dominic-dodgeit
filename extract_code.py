import os

# 这是你刚才保存的包含了所有代码的文本文件
INPUT_FILE = "all_code.txt"

def extract_files():
    if not os.path.exists(INPUT_FILE):
        print(f"错误: 找不到文件 {INPUT_FILE}。请确保你把它保存在了当前目录下。")
        return

    with open(INPUT_FILE, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    current_file = None
    file_content =[]

    for line in lines:
        # 检测文件名的标记
        if line.startswith("FILE: ./"):
            # 如果之前已经在读取某个文件了，把它保存到硬盘
            if current_file:
                save_file(current_file, file_content)
            
            # 开始记录新文件
            current_file = line.strip().split("FILE: ./")[1]
            file_content =[]
            
        # 忽略各种分割线和杂音
        elif line.startswith("===") or line.startswith("--- START OF FILE") or line.startswith("Writing fully commented"):
            continue
        
        # 写入代码正文
        else:
            if current_file is not None:
                # 为了防止文件开头的多余空行，如果是文件的第一行且为空行，跳过
                if not file_content and line.strip() == "":
                    continue
                file_content.append(line)

    # 循环结束后，保存最后一个文件
    if current_file:
        save_file(current_file, file_content)

    print("\n🎉 大功告成！所有 Java 文件已经成功提取并覆盖！")

def save_file(filename, content):
    # 去除文件末尾多余的空行
    while content and content[-1].strip() == "":
        content.pop()
        
    with open(filename, 'w', encoding='utf-8') as out_f:
        out_f.writelines(content)
    print(f"✔️  已成功写入/覆盖: {filename}")

if __name__ == "__main__":
    print("🚀 开始解析 all_code.txt 并提取文件...\n")
    extract_files()