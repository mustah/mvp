export const statusClassName = (percent: number): string => {
  if (percent >= 100) {
    return 'ok';
  } else if (percent > 70) {
    return 'warning';
  } else {
    return 'error';
  }
};