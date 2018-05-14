export const toggle = <T>(item: T, list: T[]): T[] => {
  const asSet = new Set(list);
  if (!asSet.delete(item)) {
    asSet.add(item);
  }
  return Array.from(asSet);
};
