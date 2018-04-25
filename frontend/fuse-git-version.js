const {execFileSync} = require('child_process');

const gitVersion = () => {
  // date is of the format: 2018-04-24
  const date = execFileSync('git', ['log', '-n1', '--pretty=format:%cd', '--date=short']).toString().trim();

  // ref is of the format: v0.0.22-26-ge4808a1
  const ref = execFileSync('git', ['describe', '--tags', '--abbrev=7']).toString().trim();

  return `${date} (${ref})`
};

module.exports = {gitVersion};
