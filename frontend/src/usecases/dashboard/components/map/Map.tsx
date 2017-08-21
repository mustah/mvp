import * as classNames from 'classnames';
import * as React from 'react';
import {Column} from '../../../layouts/components/column/Column';
import './Map.scss';

export interface MapProps {
  name?: string;
}

export const Map = (props: MapProps) => {
  return (
    <Column className={classNames('Map Column-center')}/>
  );
};
