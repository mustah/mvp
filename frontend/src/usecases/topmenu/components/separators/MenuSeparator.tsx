import * as classNames from 'classnames';
import * as React from 'react';
import {ClassNamed, Selectable} from '../../../../types/Types';
import './MenuSeparator.scss';

export const MenuSeparator = (props: Selectable & ClassNamed) => {
  const {isSelected, className} = props;
  return (
    <div className={classNames('MenuSeparator', className, {isSelected})}/>
  );
};
