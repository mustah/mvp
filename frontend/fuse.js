const {
  FuseBox,
  SVGPlugin,
  CSSPlugin,
  PostCSSPlugin,
  QuantumPlugin,
  SassPlugin,
  TypeScriptHelpers,
  WebIndexPlugin,
  Sparky
} = require('fuse-box');

const autoprefixer = require('autoprefixer');
const TypeHelper = require('fuse-box-typechecker').TypeHelper;
const {runCLI} = require('jest');

const distDir = 'dist';
const homeDir = 'src';
const appCss = 'css/app.css';

let fuse, app, vendor, isProduction = false;

const typeHelper = TypeHelper({
  tsConfig: './tsconfig.json',
  basePath: './',
  tsLint: './tslint.json',
  name: 'App type checker'
});

/**
 * Run linting and tests.
 */
const onBeforeRun = () => {
  typeHelper.runSync();
  runCLI({bail: isProduction}, ['src']);
};

const materialDesign = './node_modules/mdi/';
const materialDesignFonts = './fonts/**/*';
const assets = ['**/*.+(svg|png|jpg|jpeg|gif)', 'assets/fonts/**/*'];

Sparky.task('config', () => {
  fuse = new FuseBox({
    debug: true,
    homeDir: homeDir,
    sourceMaps: !isProduction,
    hash: isProduction,
    output: `${distDir}/$name.js`,
    plugins: [
      TypeScriptHelpers(),
      SVGPlugin(),
      [
        SassPlugin({outputStyle: 'compressed',}),
        PostCSSPlugin({plugins: [autoprefixer(),]}),
        CSSPlugin({
          group: `${appCss}`,
          outFile: `${distDir}/${appCss}`,
        }),
      ],
      WebIndexPlugin({template: `${homeDir}/index.html`}),
      isProduction && QuantumPlugin({
        removeExportsInterop: false,
        uglify: true,
      }),
    ]
  });

  vendor = fuse.bundle('vendor').instructions('~ index.ts');

  app = fuse.bundle('app').instructions('> index.tsx');
});

Sparky.task('watch:assets', () => Sparky.watch(assets, {base: homeDir}).dest(distDir));

Sparky.task('copy:assets', () => Sparky.src(assets, {base: homeDir}).dest(distDir));

Sparky.task('copy:external-assets', () => Sparky.src([materialDesignFonts], {base: materialDesign}).dest(distDir));

Sparky.task('clean', () => Sparky.src(distDir).clean(distDir));

Sparky.task('prod-env', ['clean'], () => isProduction = true);

Sparky.task('default', ['clean', 'config', 'copy:assets', 'copy:external-assets'], () => {
  fuse.dev();
  app.watch()
    .hmr()
    .completed(onBeforeRun);
  return fuse.run();
});

const distTasks = ['prod-env', 'config', 'copy:assets', 'copy:external-assets'];

Sparky.task('dist', distTasks, () => {
  onBeforeRun();
  return fuse.run();
});

Sparky.task('dist-server', distTasks, () => {
  fuse.dev();
  onBeforeRun();
  return fuse.run();
});
