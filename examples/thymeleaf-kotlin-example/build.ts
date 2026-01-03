#!/usr/bin/env bun

import { watch } from "fs";
import { resolve, relative, dirname, basename } from "path";
import { glob } from "glob";

const SRC_DIRS = ["src/main/kotlin", "src/main/java"];
const OUT_DIR = "src/main/resources/static/js/components";
const WATCH_MODE = process.argv.includes("--watch");

/**
 * Converts a component TypeScript file path to its output JavaScript path
 * Example: src/main/kotlin/com/example/SimpleComponent.ts
 *       -> src/main/resources/static/js/components/com/example/SimpleComponent.js
 */
function getOutputPath(inputPath: string): string {
  // Find which source directory this file is in
  const srcDir = SRC_DIRS.find((dir) => inputPath.startsWith(dir));
  if (!srcDir) {
    console.warn(`Warning: ${inputPath} is not in any source directory`);
    return "";
  }

  // Get the relative path from the source directory
  const relativePath = inputPath.substring(srcDir.length + 1);

  // Replace .ts with .js and place in output directory
  const outputPath = relativePath.replace(/\.ts$/, ".js");
  return `${OUT_DIR}/${outputPath}`;
}

/**
 * Compile a single TypeScript file using Bun
 */
async function compileFile(inputPath: string): Promise<void> {
  const outputPath = getOutputPath(inputPath);
  if (!outputPath) return;

  try {
    console.log(`Compiling: ${inputPath} -> ${outputPath}`);

    const result = await Bun.build({
      entrypoints: [inputPath],
      outdir: dirname(outputPath),
      naming: basename(outputPath),
      target: "browser",
      minify: process.env.NODE_ENV === "production",
      sourcemap: "external",
    });

    if (!result.success) {
      console.error(`Failed to compile ${inputPath}:`);
      result.logs.forEach((log) => console.error(log));
    } else {
      console.log(`✓ Successfully compiled ${inputPath}`);
    }
  } catch (error) {
    console.error(`Error compiling ${inputPath}:`, error);
  }
}

/**
 * Find all TypeScript files in source directories
 */
async function findTypeScriptFiles(): Promise<string[]> {
  const patterns = SRC_DIRS.map((dir) => `${dir}/**/*.ts`);
  const files: string[] = [];

  for (const pattern of patterns) {
    const matches = await glob(pattern, { ignore: "node_modules/**" });
    files.push(...matches);
  }

  return files;
}

/**
 * Compile all TypeScript files
 */
async function compileAll(): Promise<void> {
  console.log("🔨 Building TypeScript components...");
  const files = await findTypeScriptFiles();

  if (files.length === 0) {
    console.log("No TypeScript files found.");
    return;
  }

  console.log(`Found ${files.length} TypeScript file(s)`);

  for (const file of files) {
    await compileFile(file);
  }

  console.log("✅ Build complete!");
}

/**
 * Watch mode: compile files on change
 */
async function watchFiles(): Promise<void> {
  console.log("👀 Watching for TypeScript changes...");

  // Initial build
  await compileAll();

  // Watch each source directory
  for (const srcDir of SRC_DIRS) {
    const watcher = watch(
      srcDir,
      { recursive: true },
      async (event, filename) => {
        if (filename && filename.endsWith(".ts")) {
          const fullPath = `${srcDir}/${filename}`;
          console.log(`\n📝 File changed: ${fullPath}`);
          await compileFile(fullPath);
        }
      }
    );
  }

  console.log("Press Ctrl+C to stop watching");
}

// Main execution
if (WATCH_MODE) {
  watchFiles();
} else {
  compileAll();
}
