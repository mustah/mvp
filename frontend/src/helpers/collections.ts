import {ObjectsById} from '../state/domain-models/domainModels';
import {Identifiable, IdNamed, uuid} from '../types/Types';

export const toggle = <T>(item: T, list: T[]): T[] => {
  const asSet = new Set(list);
  if (!asSet.delete(item)) {
    asSet.add(item);
  }
  return Array.from(asSet);
};

export const getId = (item: IdNamed | Identifiable): uuid => item.id;

export const groupById = <T extends Identifiable>(items: T[]): ObjectsById<T> =>
  items.reduce(
    (all: ObjectsById<T>, current: T) => {
      all[current.id] = current;
      return all;
    },
    {}
  );
