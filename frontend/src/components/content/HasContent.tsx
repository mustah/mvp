import * as React from 'react';
import {HasContent as HasContentType} from '../../types/Types';

interface Props extends HasContentType {
  fallbackContent: React.ReactElement<any>;
  children: React.ReactElement<any>;
}

export const HasContent = ({hasContent, fallbackContent, children}: Props) =>
  hasContent ? children : fallbackContent;
