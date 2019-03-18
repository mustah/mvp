import * as React from 'react';

interface Props {
  resized: boolean;
  height: number;
}

export const useResizeWindow = (): Props => {
  const [resized, setResized] = React.useState<boolean>(false);
  const [height, setHeight] = React.useState<number>(window.innerHeight);
  const updateDimensions = () => {
    setResized(!resized);
    setHeight(window.innerHeight);
  };
  React.useEffect(() => {
    window.addEventListener('resize', updateDimensions);
    return () => {
      window.removeEventListener('resize', updateDimensions);
    };
  });

  return {resized, height};
};
