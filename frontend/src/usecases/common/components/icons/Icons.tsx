import * as classNames from 'classnames';
import * as React from 'react';
import {ClassNamed} from '../../../../types/Types';
import {Row} from '../layouts/row/Row';
import './Icons.scss';

export interface IconProps extends ClassNamed {
  name: string;
  size?: string;
}

export const Icon = (props: IconProps) => {
  const {className, name, size} = props;
  return (
    <Row className={classNames('Row-center Icon', className)}>
      <i className={classNames(`mdi mdi-${name}`, size)}/>
    </Row>
  );
};
