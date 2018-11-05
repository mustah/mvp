import * as React from 'react';

declare module 'react' {

  type StateAction<S> = <S>(newState: S) => void;
  type InstanceFactory<T> = <T>() => T;

  function useState<S>(initialState: S | InstanceFactory<S>): [S, StateAction<S>];

  function useEffect(
    create: () => void,
    inputs?: ReadonlyArray<any>
  ): void;

  function useReducer<S, A>(
    reducer: (state: S, action: A) => S,
    initialState: S
  ): [S, (action: A) => void];

  function useCallback<F extends (...args: never[]) => any>(
    callback: F,
    inputs?: ReadonlyArray<any>
  ): F;

  function useMemo<T>(create: InstanceFactory<T>, inputs?: ReadonlyArray<any>): T;

  function useRef<T extends {}>(initialValue?: T): React.Ref<T>;

  function useImperativeMethods<T>(
    ref: React.Ref<T>,
    createInstance: InstanceFactory<T>,
    inputs?: ReadonlyArray<any>
  ): void;

  const useMutationEffect: typeof useEffect;
  const useLayoutEffect: typeof useEffect;
}
