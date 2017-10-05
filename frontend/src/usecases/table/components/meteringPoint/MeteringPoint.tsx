import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../../app/routes';

interface MeteringPointProps {
  id: string;
}

export const MeteringPoint = (props: MeteringPointProps) => (
  <div>
    <Link to={routes.mp + '/' + props.id}>{props.id}</Link>
  </div>);
