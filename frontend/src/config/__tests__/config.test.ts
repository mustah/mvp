import {LazyAppConfig} from '../AppConfig';
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

  it('is a function that returns an object of configuration settings', () => {
    const config = configByEnvironment('development');
    expect(config).toBeInstanceOf(Function);
    const actualValues = config();
    expect(actualValues).toHaveProperty('environment');
  });

  it('reacts to development environment', () => {
    const actualValues = configByEnvironment('development')();
    expect(actualValues).toHaveProperty('environment');
    expect(actualValues.environment).toEqual('development');
  });

  it('reacts to production environment', () => {
    const actualValues = configByEnvironment('production')();
    expect(actualValues).toHaveProperty('environment');
    expect(actualValues.environment).toEqual('production');
  });

  it('defaults to production environment if no environment given', () => {
    const actualValues = configWithoutEnvironment()();
    expect(actualValues).toHaveProperty('environment');
    expect(actualValues.environment).toEqual('production');
  });

  it('defaults to production environment if invalid environment given', () => {
    const actualValues = configByEnvironment('hakuna matata robocop marshmallow')();
    expect(actualValues).toHaveProperty('environment');
    expect(actualValues.environment).toEqual('production');
  });
});
