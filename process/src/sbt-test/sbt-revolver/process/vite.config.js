import { defineConfig } from "vite";
import scalaJSPlugin from "@scala-js/vite-plugin-scalajs";

export default defineConfig({
  plugins: [
    scalaJSPlugin({
      // URI prefix of imports that this plugin catches (without the trailing ':')
      // default: 'scalajs' (so the plugin recognizes URIs starting with 'scalajs:')
      uriPrefix: "scalajs",
    }),
  ],
});
