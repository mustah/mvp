import * as React from 'react';
import {Foldable, FoldableProps} from './Foldable';
import './FoldableMenuItem.scss';

export const FoldableMenuItem = (props: FoldableProps) =>
  <Foldable {...props} containerClassName="FoldableMenuItem"/>;
