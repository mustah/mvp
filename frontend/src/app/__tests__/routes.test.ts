import {getLogoPath} from '../routes';

describe('routes', () => {
  it('returns a company logo path', () => {
    const company = 'wayne-industries';

    const logoPath = getLogoPath(company);

    expect(logoPath).toEqual('wayne-industries.png');
  });

  it('returns the default logo path since company doesnt have their own logo', () => {
    const company = 'star-wars-company';

    const logoPath = getLogoPath(company);

    expect(logoPath).toEqual('elvaco_logo.png');
  });
});
