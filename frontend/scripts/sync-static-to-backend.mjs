import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const frontendWebDir = path.resolve(__dirname, "../web");
const backendStaticDir = path.resolve(__dirname, "../../backend/src/main/resources/static");

function ensureDir(dir) {
  fs.mkdirSync(dir, { recursive: true });
}

function cleanDir(dir) {
  if (!fs.existsSync(dir)) return;
  for (const entry of fs.readdirSync(dir)) {
    const fullPath = path.join(dir, entry);
    fs.rmSync(fullPath, { recursive: true, force: true });
  }
}

function copyDir(src, dest) {
  fs.cpSync(src, dest, { recursive: true, force: true });
}

if (!fs.existsSync(frontendWebDir)) {
  throw new Error(`frontend web source not found: ${frontendWebDir}`);
}

ensureDir(backendStaticDir);
cleanDir(backendStaticDir);
copyDir(frontendWebDir, backendStaticDir);

console.log("Synced frontend/web -> backend/src/main/resources/static");
