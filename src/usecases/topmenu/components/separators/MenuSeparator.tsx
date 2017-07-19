import * as React from 'react';
import {ClassNamed, Selectable} from '../../../../types/Types';
import './MenuSeparator.scss';

export const MenuSeparator = (props: Selectable & ClassNamed) => {
  const {isSelected} = props;
  const selectedClassName = (isSelected && 'isSelected') || '';
  return (
    <div className={`MenuSeparator ${selectedClassName} ${props.className || ''}`}/>
  );
};
