const {exec, execFileSync} = require('child_process');

const appJs = `./dist/src.*.js`;

const gitVersion = async () => {
  try {
    // date is of the format: 2018-04-24
    const date = execFileSync('git', ['log', '-n1', '--pretty=format:%cd', '--date=short']).toString().trim();

    // ref is of the format: v0.0.22-26-ge4808a1
    const ref = execFileSync('git', ['describe', '--tags', '--abbrev=7']).toString().trim();

    await exec(`sed -i '' 's/FRONTEND_VERSION/${`${date} (${ref})`}/' ${appJs}`);

    process.exit(0);
  } catch (error) {
    console.error('Unable to replace version due to: ', error);
    process.exit(1);
  }
};

module.exports = {gitVersion};
