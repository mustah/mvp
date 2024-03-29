import {SvgIconProps} from 'material-ui';
import SvgIcon from 'material-ui/SvgIcon';
import * as React from 'react';
import {colors} from '../../app/colors';

/* tslint:disable */
export const IconGas = (props: SvgIconProps) => (
  <SvgIcon viewBox="0 0 100 100" {...props} color={props.color || colors.white}>
    <path d="M49.8,61c0.5,0,1,0.1,1.5,0.1c0.1,0,0.1,0,0.2,0c6.8,0,13.2-3.6,16.9-9.3c4.1-6.5,3.6-15-0.1-21.5
	c-2.7-4.7-8.4-7.8-9.3-13.5c-0.8-5.2,1.6-10.2,4.7-14.1c-0.3,0.4-2.1,0.5-2.6,0.7c-0.9,0.3-1.9,0.6-2.8,0.9
	c-1.7,0.6-3.4,1.3-5.1,2.1c-2.6,1.2-5,2.7-7.3,4.5c-11.5,9.1-19.2,26.8-11.6,40.6C37.5,57.1,43.3,60.5,49.8,61 M50,15.9
	c0.7-0.5,1.4-1.1,2.2-1.6c0,1.2,0.1,2.3,0.2,3.5c0.9,5.5,4.3,9.1,7,11.9c1.3,1.3,2.4,2.6,3.1,3.7c2.8,4.9,2.9,10.7,0.3,14.8
	c-2.5,3.8-6.8,6.2-11.3,6.2l-0.1,0c-0.3,0-0.7,0-1,0c-4.5-0.4-8.2-2.7-10.2-6.3C34.3,37.7,40.7,23.3,50,15.9"/>
    <path d="M51.8,51c0.2,0,0.5,0,0.7-0.1c2.4-0.4,3.9-2.6,3.5-5c-0.4-2.1-2.2-3.6-4.3-3.6c-0.2,0-0.5,0-0.7,0.1
	c-2.4,0.4-3.9,2.6-3.5,5C47.9,49.5,49.7,51,51.8,51"/>
    <polygon points="63.4,88.4 63.4,79.4 70.9,79.4 70.9,72.8 63.4,72.8 63.4,63.4 39,63.4 39,88.4 18.3,88.4 18.3,95
	45.6,95 45.6,70 56.8,70 56.8,95 84.8,95 84.8,88.4 "/>
  </SvgIcon>
);
/* tslint:enable */
