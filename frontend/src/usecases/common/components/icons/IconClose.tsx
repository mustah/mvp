import * as React from 'react';
import {Clickable} from '../../../../types/Types';
import {Icon} from './Icon';

export const CloseIconButton = (props: Clickable) => (
  <Icon
    onClick={props.onClick}
    name="close"
    size="large"
    className="Icon-Button"
  />
);
