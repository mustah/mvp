import {SvgIconProps} from 'material-ui';
import {ActionHelpOutline} from 'material-ui/svg-icons';
import * as React from 'react';

export const IconUnknown = (props: SvgIconProps) => (
  <ActionHelpOutline color={props.color || 'white'} style={props.style}/>
);
