import * as React from 'react';
import {Expandable} from '../../../types/Types';
import {Icon} from '../../common/components/icons/Icons';
import './LinkItem.scss';

export interface NavigationItemProps extends Expandable {
  icon: string;
  name: string;
}

export const NavigationItem = (props: NavigationItemProps) => {
  return (
    <Icon/>
  );
};
