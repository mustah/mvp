import {default as classNames} from 'classnames';
import * as React from 'react';
import {Callback, ClassNamed, WithChildren} from '../../../types/Types';
import {IconRightArrow} from '../../icons/IconRightArrow';
import {BoldFirstUpper} from '../../texts/Texts';
import {Column} from '../column/Column';
import {RowMiddle} from '../row/Row';
import './Foldable.scss';

interface Visible {
  isVisible: boolean;
}

export interface FoldableProps extends ClassNamed, WithChildren, Partial<Visible> {
  title: string;
  containerClassName?: string;
}

interface ToggleVisibilityProps extends Visible {
  showHide: Callback;
}

const useToggleVisibility = (initialState: boolean): ToggleVisibilityProps => {
  const [isVisible, toggle] = React.useState(initialState);
  const showHide = () => toggle(!isVisible);
  return {isVisible, showHide};
};

export const Foldable = ({
  children,
  className,
  containerClassName,
  title,
  isVisible: initialVisibility = true
}: FoldableProps) => {
  const {isVisible, showHide} = useToggleVisibility(initialVisibility);

  return (
    <Column className={classNames('Foldable', containerClassName)}>
      <RowMiddle className="Foldable-title">
        <IconRightArrow onClick={showHide} className={classNames('Foldable-arrow', {isVisible})}/>
        <BoldFirstUpper className="Medium">{title}</BoldFirstUpper>
      </RowMiddle>
      <div className={classNames('Foldable-content', className, {isVisible})}>
        {children}
      </div>
    </Column>
  );
};
