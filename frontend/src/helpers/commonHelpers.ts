import {split, trimEnd, uniq} from 'lodash';

export const isDefined = (item: any): boolean => item !== undefined;

export const identity = (input: any): any => input;

export const identityType = <T>(input: T): T => input;

export const noop = (): void => {
}; // tslint:disable-line:no-empty

export const fromCommaSeparated = (payload: string): string[] =>
  uniq(split(payload, ',')
    .map(it => trimEnd(it.trim(), '\n'))
    .filter(it => it.length > 0)
  );

export const isEnter = ev => ev.key === 'Enter';
