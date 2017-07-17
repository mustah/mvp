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

const distDir = 'dist';
const homeDir = 'src';
const tmpDir = '.tmp';
const appCss = 'app.css';

let fuse, app, vendor, isProduction = false;

const typeHelper = TypeHelper({
  tsConfig: './tsconfig.json',
  basePath: './',
  tsLint: './tslint.json',
  name: 'App type checker'
});

const assets = ['assets/images/**/*.+(svg|png|jpg|jpeg|gif)', 'assets/fonts/*'];

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

Sparky.task('clean', () => Sparky.src(distDir).clean(distDir));

Sparky.task('prod-env', ['clean'], () => isProduction = true);

Sparky.task('default', ['clean', 'config', 'copy:assets'], () => {
  fuse.dev();
  app.watch()
    .hmr()
    .completed(process => typeHelper.runSync());
  return fuse.run();
});

const distTasks = ['prod-env', 'config', 'copy:assets'];

Sparky.task('dist', distTasks, () => {
  typeHelper.runSync();
  return fuse.run();
});

Sparky.task('dist-server', distTasks, () => {
  fuse.dev();
  typeHelper.runSync();
  return fuse.run();
});
