import * as React from 'react';
import {Image} from '../../../common/components/images/Image';
import {Column} from '../../../common/components/layouts/column/Column';
import './Map.scss';

export interface MapProps {
  name?: string;
}

export const Map = (props: MapProps) => {
  return (
    <Column className="Map">
      <Image src="usecases/dashboard/img/map.png"/>
    </Column>
  );
};
