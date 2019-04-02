export const thresholdClassName = (percent: number): string => {
  if (percent >= 100) {
    return 'ok';
  } else if (percent > 70) {
    return 'warning';
  } else if (isNaN(percent)) {
    return 'info';
  } else {
    return 'error';
  }
};
