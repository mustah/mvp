import {default as classNames} from 'classnames';
import * as React from 'react';
import {ClassNamed, Selectable} from '../../../types/Types';

export const TabUnderline = (props: Selectable & ClassNamed) => {
  const {isSelected, className} = props;
  return (
    <div className={classNames('TabUnderline', className, {isSelected})}/>
  );
};
