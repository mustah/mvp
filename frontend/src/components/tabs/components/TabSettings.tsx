import * as React from 'react';
import {Children} from '../../../types/Types';
import {Column} from '../../layouts/column/Column';
import {TabUnderline} from './TabUnderliner';

interface Props {
  children: Children;
}

export const TabSettings = ({children}: Props) => (
  <Column className="TabSettings">
    {children}
    <TabUnderline/>
  </Column>
);
