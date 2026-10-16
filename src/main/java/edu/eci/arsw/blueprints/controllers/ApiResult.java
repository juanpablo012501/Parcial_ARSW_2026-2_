package edu.eci.arsw.blueprints.controllers;

public record ApiResult<T>(int code, String message, T data) {
}
