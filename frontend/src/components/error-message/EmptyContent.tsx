import ActionFace from 'material-ui/svg-icons/action/face';
import * as React from 'react';
import {colors} from '../../app/colors';
import {ColumnCenter} from '../layouts/column/Column';
import {Large} from '../texts/Texts';
import './EmptyContent.scss';

export interface EmptyContentProps {
  noContentText: string;
}

const style: React.CSSProperties = {width: 52, height: 52, marginBottom: '24px'};

export const EmptyContent = ({noContentText}: EmptyContentProps) => (
  <ColumnCenter className="EmptyContent">
    <ActionFace style={style} color={colors.borderColor}/>
    <Large className="Bold">{noContentText}</Large>
  </ColumnCenter>
);
