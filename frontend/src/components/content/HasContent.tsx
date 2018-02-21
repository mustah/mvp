import * as React from 'react';

interface Props {
  hasContent: boolean;
  fallbackContent: React.ReactElement<any>;
  className?: string;
  children: React.ReactElement<any>;
}

export const HasContent = (props: Props) => {
  const {hasContent, fallbackContent, children} = props;

  if (!hasContent) {
    return (fallbackContent);
  } else {
    return children;
  }
};
