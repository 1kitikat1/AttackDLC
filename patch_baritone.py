import os

path = r"C:\Users\Kitikat\Desktop\AttackDLC\src\main\java\org\attackdlc\pve\navigation\BaritoneNavigator.java"
with open(path, "r", encoding="utf-8-sig") as f:
    content = f.read()

print(f"Original lines: {content.count(chr(10))}")
print(f"Has BOM: {content.startswith(chr(0xFEFF))}")

# 在 begin() 方法中添加 deleteBaritoneCache() 调用
old_line = '      Objects.requireNonNull(options, "options");'
new_lines = '''      // 删除旧的 baritone 缓存——Baritone 初始化时会重新创建，但不留旧数据
      deleteBaritoneCache();
      Objects.requireNonNull(options, "options");'''
content = content.replace(old_line, new_lines, 1)

# 在文件末尾 } 之前插入 deleteBaritoneCache 方法
method_code = '''
   /** 删除 baritone 缓存文件夹，防止残留数据。 */
   private static void deleteBaritoneCache() {
      try {
         java.nio.file.Path d = java.nio.file.Paths.get("baritone");
         if (java.nio.file.Files.isDirectory(d)) {
            java.nio.file.Files.walk(d)
                   .sorted((a, b) -> b.compareTo(a))
                   .forEach(p -> { try { java.nio.file.Files.delete(p); } catch (Exception ignored) {} });
         }
      } catch (Exception ignored) {}
   }
'''
last_brace = content.rfind("}")
if last_brace > 0:
    content = content[:last_brace] + method_code + "\n" + content[last_brace:]

# 写回文件（无 BOM）
with open(path, "w", encoding="utf-8", newline="") as f:
    f.write(content)

# 验证
with open(path, "r", encoding="utf-8") as f:
    v = f.read()
print(f"Has deleteBaritoneCache call: {'deleteBaritoneCache()' in v}")
print(f"Has deleteBaritoneCache def: {'private static void deleteBaritoneCache' in v}")
print(f"New line count: {v.count(chr(10))}")
