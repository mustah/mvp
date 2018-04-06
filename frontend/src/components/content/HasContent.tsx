import * as React from 'react';

interface Props {
  hasContent: boolean;
  fallbackContent: React.ReactElement<any>;
  children: React.ReactElement<any>;
}

export const HasContent = ({hasContent, fallbackContent, children}: Props) =>
  (hasContent ? children : fallbackContent);
