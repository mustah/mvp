import * as React from 'react';
import {dropdownStyle} from '../../app/themes';
import {Column} from '../layouts/column/Column';
import {BoldFirstUpper, FirstUpper} from './Texts';

interface Props {
  name: string;
  subTitle: string;
}

export const CityInfo = ({name, subTitle}: Props) => (
  <Column>
    <BoldFirstUpper>{name}</BoldFirstUpper>
    <FirstUpper style={dropdownStyle.parentStyle}>{subTitle}</FirstUpper>
  </Column>
);

export const LabelWithSubtitle = ({name, subTitle}: Props) => (
  <Column>
    <FirstUpper>{name}</FirstUpper>
    <FirstUpper style={dropdownStyle.parentStyle}>{subTitle}</FirstUpper>
  </Column>
);
