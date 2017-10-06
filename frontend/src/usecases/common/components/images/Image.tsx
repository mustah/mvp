import * as React from 'react';
import './Image.scss';

interface ImageProps {
  src: string;
}

export const Image = (props: ImageProps) =>
  (<img src={props.src} className="Image"/>);
