import * as classNames from 'classnames';
import * as React from 'react';
import {ClassNamed} from '../../../../types/Types';
import {Row} from '../layouts/row/Row';
import './Icon.scss';

type IconSize = 'small' | 'normal' | 'medium' | 'large';

export interface IconProps extends ClassNamed {
  name: string;
  size?: IconSize;
  onClick?: (...args) => void;
}

export const Icon = (props: IconProps) => {
  const {className, name, onClick, size} = props;
  return (
    <Row className={classNames('Row-center Icon', className)} onClick={onClick}>
      <i className={classNames(`mdi mdi-${name}`, size)}/>
    </Row>
  );
};
