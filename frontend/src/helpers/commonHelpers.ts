export const isDefined = (item: any): boolean => item !== undefined;

export const isNotNull = (item: any): boolean => item !== null;

export const identity = (input: any): any => input;

export const identityType = <T>(input: T): T => input;

export const noop = (): void => {
}; // tslint:disable-line:no-empty
