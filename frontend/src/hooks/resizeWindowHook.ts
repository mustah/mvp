import * as React from 'react';

interface Props {
  resized: boolean;
}

export const useResizeWindow = (): Props => {
  const [resized, setResized] = React.useState<boolean>(false);
  const updateDimensions = () => setResized(!resized);

  React.useEffect(() => {
    window.addEventListener('resize', updateDimensions);
    return () => {
      window.removeEventListener('resize', updateDimensions);
    };
  });

  return {resized};
};
