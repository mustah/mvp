import {default as classNames} from 'classnames';
import * as React from 'react';
import {ClassNamed, Visible, WithChildren} from '../../../types/Types';
import {IconRightArrow} from '../../icons/IconRightArrow';
import {BoldFirstUpper} from '../../texts/Texts';
import {Column} from '../column/Column';
import {Row, RowMiddle} from '../row/Row';
import './Foldable.scss';
import {useToggleVisibility} from './foldableHook';

export interface FoldableProps extends ClassNamed, WithChildren, Visible {
  title: string;
  containerClassName?: string;
  fontClassName?: string;
}

export const Foldable = ({
  children,
  className,
  containerClassName,
  fontClassName = 'Medium',
  title,
  isVisible: initialVisibility = true
}: FoldableProps) => {
  const {isVisible, showHide} = useToggleVisibility(initialVisibility);

  return (
    <Column className={classNames('Foldable', containerClassName)}>
      <RowMiddle onClick={showHide} className={classNames('Foldable-title', 'clickable')}>
        <IconRightArrow className={classNames('Foldable-arrow', {isVisible})}/>
        <BoldFirstUpper className={fontClassName}>{title}</BoldFirstUpper>
      </RowMiddle>
      <Row className={classNames('Foldable-content', className, {isVisible})}>
        {children}
      </Row>
    </Column>
  );
};

export const FoldableMenuItem = (props: FoldableProps) =>
  <Foldable containerClassName="FoldableMenuItem" fontClassName="Normal" {...props}/>;
