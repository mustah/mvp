import {getLogoPath} from '../routes';

describe('routes', () => {

  it('returns a organisation logo path', () => {
    expect(getLogoPath('wayne-industries')).toEqual('assets/images/wayne-industries.png');
  });

  it('returns the default logo path since organisation doesnt have their own logo', () => {
    expect(getLogoPath('star-wars')).toEqual('assets/images/elvaco_logo.png');
  });
});
