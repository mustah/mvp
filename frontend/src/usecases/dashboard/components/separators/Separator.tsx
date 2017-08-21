import * as classNames from 'classnames';
import * as React from 'react';
import {ClassNamed} from '../../../../types/Types';
import './Separator.scss';

export const Separator = (props: ClassNamed) => {
  const {className} = props;
  return (
    <div className={classNames('Separator', className)}/>
  );
};
