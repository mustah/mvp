import {AppConfig, LazyAppConfig} from '../AppConfig';
import {config} from '../config';

/**
 * Some of these tests needs to be updated based on actual values
 * used in the production, a typically bad smell for a test. I
 * still think that we get value out of these tests, because
 * TypeScript did not notice type related errors in the config
 * switching between prod <=> dev, resulting in run time errors.
 */
describe('Configuration', () => {

  const originalEnv = {...process.env};

  const configByEnvironment = (env: string): LazyAppConfig => {
    process.env.NODE_ENV = env;
    return config;
  };

  const configWithoutEnvironment = (): LazyAppConfig => {
    delete process.env.NODE_ENV;
    return config;
  };

  beforeEach(() => {
    process.env = originalEnv;
  });

  afterEach(() => {
    process.env = originalEnv;
  });

  it('has to be executed in order to retrieve actual values', () => {
    const config: LazyAppConfig = configByEnvironment('development');

    expect(config()).toHaveProperty('environment');
  });

  it('reacts to development environment', () => {
    const actualValues: AppConfig = configByEnvironment('development')();

    expect(actualValues.environment).toEqual('development');
  });

  it('reacts to production environment', () => {
    const actualValues: AppConfig = configByEnvironment('production')();

    expect(actualValues.environment).toEqual('production');
  });

  it('defaults to production environment if no environment given', () => {
    const actualValues: AppConfig = configWithoutEnvironment()();

    expect(actualValues.environment).toEqual('production');
  });

  it('defaults to production environment if invalid environment given', () => {
    const actualValues: AppConfig = configByEnvironment('hakuna matata robocop marshmallow')();

    expect(actualValues.environment).toEqual('production');
  });
});
